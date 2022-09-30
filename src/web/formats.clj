(ns web.formats
  (:require [ring.util.codec]
            [ring.middleware.multipart-params :as multi]
            [clojure.edn :as edn]
            [ring.middleware.multipart-params.byte-array :as ba]
            [cheshire.core :as json]
            [cheshire.generate :as json-gen]
            [clojure.pprint :as pprint]
            [ring.util.io]
            [clj-time.format :as tfmt]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [cognitect.transit :as transit]
            [clojure.walk])
  (:import com.fasterxml.jackson.core.JsonParseException
           (clojure.lang Keyword Var)
           [com.fasterxml.jackson.core JsonGenerator]
           (com.zaxxer.hikari HikariDataSource)
           [java.io BufferedWriter ByteArrayOutputStream InputStream OutputStreamWriter ByteArrayInputStream PushbackReader]
           (java.util ArrayList LinkedHashMap LinkedHashSet)
           (org.httpkit.server AsyncChannel)
           (org.joda.time DateTime)
           (org.postgresql.util PGobject)))

(json-gen/add-encoder AsyncChannel json-gen/encode-str)
(json-gen/add-encoder Var json-gen/encode-str)
(json-gen/add-encoder HikariDataSource json-gen/encode-str)
(json-gen/add-encoder PGobject json-gen/encode-str)
(json-gen/add-encoder Object json-gen/encode-str)

(def date-to-json-formatter (tfmt/formatters :date-time))

(json-gen/add-encoder
  DateTime
  (fn  [d json-generator]
   (.writeString ^JsonGenerator json-generator (tfmt/unparse date-to-json-formatter d))))

(defn generate-stream
  ([data] (generate-stream data nil))
  ([data options]
   (ring.util.io/piped-input-stream
    (fn [out] (json/generate-stream
               data (-> out (OutputStreamWriter.) (BufferedWriter.)) options)))))


(defmulti do-format (fn [fmt _ _pretty? ] fmt))

(defmethod do-format :json [_ body pretty?]
  (generate-stream body {:pretty pretty?}))

(defmethod do-format :text [_ body _] body)

(defmethod do-format :edn [_ body pretty? ]
  (if pretty?
    (with-out-str (pprint/pprint body))
    (with-out-str (pr body))))

(defmethod do-format :transit [_ body _] ;; (transit always ugly)
  (ring.util.io/piped-input-stream
   (fn [out] (transit/write (transit/writer out :json) body))))

(defmulti parse-format (fn [fmt _ _] fmt))

(defmethod parse-format :transit [_ _ {b :body}]
  (when b
    (let [r (cond (string? b) (transit/reader (ByteArrayInputStream. (.getBytes ^String b)) :json)
                  (instance? InputStream b) (transit/reader b :json))]
      {:resource (transit/read r)})))

(defn parse-json [content]
  (when content
    {:resource (cond
                 (string? content) (json/parse-string content keyword)
                 (instance? InputStream content) (json/parse-stream (io/reader content) keyword)
                 :else content)}))

(defmethod parse-format :json [_ _ {b :body}]
  (parse-json b))

(defmethod parse-format :edn [_ _ {b :body}]
  (when b
    {:resource
     (cond
       (string? b) (edn/read-string b)
       (instance? InputStream b)
       (-> b
           io/reader
           PushbackReader.
           edn/read)
       :else b)}))

(defmethod parse-format :form-data [_ _ req]
  {:form-params (clojure.walk/keywordize-keys (:multipart-params (multi/multipart-params-request req {:store (ba/byte-array-store)})))})

(defmethod parse-format :text [_ct _ {^String b :body}]
  (when b
    {:text
     (cond
       (string? b) b
       (instance? InputStream b)
       (slurp b :encoding "UTF-8"))}))

(defmethod parse-format :ndjson [_ct _ {b :body}]
  (when b
    {:body b}))

(defmethod parse-format nil [_ct _ {b :body}]
  (try (parse-json b)
       (catch JsonParseException e
         (throw (ex-info (format "`content-type` header is missing, body parsed as JSON by default and got this error:\n%s\nProvide appropriate content-type for your body or valid JSON" (ex-message e)) {})))))

(defmethod parse-format :default [ct _ _]
  (throw (RuntimeException. (str "Unknown/not supported Content-Type: " ct))))

(defn form-decode [s] (clojure.walk/keywordize-keys (ring.util.codec/form-decode s)))

(defmethod parse-format :query-string [_ _ {b :body}]
  (when-let [b (cond
                 (string? b) b
                 (instance? InputStream b)
                 (if (pos? (.available ^InputStream b))
                   (slurp (io/reader b))
                   nil)
                 :else nil)]
    {:form-string b
     :form-params (form-decode b)}))

(def ct-mappings
  {"application/json" :json
   "application/json+fhir" :json
   "application/fhir+json" :json
   "application/json-patch+json" :json
   "application/merge-patch+json" :json
   "application/fhir+ndjson" :ndjson
   "application/ndjson" :ndjson
   "application/x-ndjson" :ndjson

   "application/transit+json" :transit
   "text/yaml" :yaml
   "text/edn" :edn
   "text/plain" :text
   "text/html" :text
   "*/*" :json
   "application/x-www-form-urlencoded" :query-string
   "multipart/form-data" :form-data
   "application/yaml" :yaml
   "application/edn" :edn

   "application/fhir+xml" :xml
   "application/cda+xml" :cda
   "application/xml" :xml})

(defn header-to-format [content-type]
  (if (str/blank? content-type)
    [nil ""]

    (let [[ct options] (->> (str/split (first (str/split content-type #",")) #"\s*;\s*")
                            (map str/trim))]
      [(get ct-mappings ct ct) options])))

(defn parse-accept-header [ct]
  (map str/trim (str/split ct #"[,;]")))

(defn get-format
  [ct]
  (if-let [fmt (ct-mappings ct)]
    fmt
    (when (str/starts-with? ct "charset")
      :charset)))

(defn append-charset
  [[fmt ac] charset]
  [fmt (str ac \; charset)])

(defn select-accept-header
  "Prioritizes json if can"
  [ct]
  (when (and ct (string? ct))
    (let [{json-cts :json
           [[_ charset]] :charset
           other-cts nil} (->> (parse-accept-header ct)
                               (map (fn [ct] [(get-format ct) ct]))
                               (filter first)
                               (group-by (comp #{:json :charset} first)))]
      (cond-> (or (first json-cts) (first other-cts))
        charset (append-charset charset)))))

(defn accept-header-to-format [ct]
  (first (select-accept-header ct)))

(defn content-type [fmt accept]
  (let [[selected-fmt selected-accept] (select-accept-header accept)]
    (if (and (= fmt selected-fmt)
             (not= "*/*" selected-accept))
      selected-accept
      (get {:edn     "text/edn"
            :json    "application/json"
            :transit "application/transit+json"} fmt))))

(def known-formats
  {"json"     :json
   "edn"      :edn
   "transit"  :transit})

(defn get-wanted-format [{{fmt :_format _pretty? :_pretty} :params {ac "accept", ct "content-type"} :headers :as _request}]
  (let [wanted-fmt
        (cond
          fmt   (or (get known-formats fmt)
                    (get ct-mappings fmt))
          ac    (accept-header-to-format ac)
          ct    (accept-header-to-format ct)
          :else :json)

        known-resp-format?
        (contains? (methods do-format) wanted-fmt)]
    (cond
      (nil? wanted-fmt)  nil
      known-resp-format? wanted-fmt
      :else              :json)))


(defn format-response
  [{body :body {ct "content-type" :as headers} :headers :as resp} request]
  (if (when-let [prf (get-in request [:headers "prefer"])] (re-find  #"return\s*=\s*minimal" prf))
    {:headers headers}
    (let [request (update-in request [:headers "accept"] (fn [ac] (if (= "*/*" ac) nil ac)))
          {{format :_format pretty? :_pretty} :params {ac "accept"} :headers} request]
      (if-not ct
        (if-let [fmt (get-wanted-format request)]
          (if (and body (or (vector? body) (map? body) (coll? body)))
            (assoc resp
                   :body    (do-format fmt body (or (= pretty? "true") (= pretty? true)))
                   :headers (assoc headers "content-type" (content-type fmt ac)))
            resp)
          {:body (str "Unknown request format - " (or format ac)". Use "
                      (cond format (str "_format = " (str/join " | " (keys known-formats)))
                            ac  (str "Accept: " (str/join " | " (keys ct-mappings)))))
           :status 422})
        resp))))


(defn add-body-bytes [config {:as req :keys [body]}]
  (cond
    (not (get config :request-save-raw-body))
    req

    (string? body) (assoc req :body-bytes (bytes (byte-array (map (comp byte int) body))))

    (instance? InputStream body)
    (let [body-byte-array-output-stream (ByteArrayOutputStream.)
          _                             (.transferTo body body-byte-array-output-stream)
          body-bytes                    (.toByteArray body-byte-array-output-stream)]
      (assoc req
             :body (ByteArrayInputStream. body-bytes)
             :body-bytes body-bytes))

    :else req))


(defn parse-body
  [{body :body {ct "content-type"} :headers {_fmt :_format} :params :as req} & [config]]
  (let [[content-type options] (header-to-format ct)]
    (when body
      (let [req-with-body-bytes (add-body-bytes config req)
            parsed-body (parse-format content-type options req-with-body-bytes)]
        (merge req-with-body-bytes parsed-body)))))
