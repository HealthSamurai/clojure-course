(ns zframes.rpc
  (:require [re-frame.db :as db]
            [re-frame.core :as rf]
            #_[jute.js :as js]))

(defonce debounce-state (atom {}))
(defonce abort-state (atom {}))

(defn to-json [x]
  #?(:cljs (js/JSON.stringify (clj->js x))))


(defn from-json [x]
  #?(:cljs (js->clj (js/JSON.parse x) :keywordize-keys true)))


(def api-base-url
  "http://localhost:7777")


(defn abort-ctrl []
  #?(:cljs (js/AbortController.)))

(defn fetch [opts cb]
  #?(:cljs
     (-> (js/fetch (str api-base-url "/rpc")
                   (clj->js {:method "post", :headers {"accept" "application/json", "Content-Type" "application/json"},
                             :body (to-json (select-keys opts [:method :params :id :policy])), :signal (:signal opts)} :keyword-fn (fn [x] (subs (str x) 1))))
         (.then
          (fn [resp]
            (.then (.text resp)
                   (fn [doc]
                     (let [cdoc (from-json doc)]
                       (if-let [res  (and (< (.-status resp) 299) (:result cdoc))]
                         (cb {:result res})
                         (cb {:error (or (:error cdoc) cdoc)})))))))
         (.catch (fn [err] (cb {:error err}))))))

(defn *rpc-fetch [{:keys [path debounce force success error transport] :as opts}]
  #?(:cljs
     (if (and debounce path (not force))
       (do
         (when-let [t (get @debounce-state path)]
           (js/clearTimeout t))
         (swap! debounce-state assoc path (js/setTimeout #(*rpc-fetch (assoc opts :force true)) debounce)))
       (let [db db/app-db
             dispatch-event (fn [event payload]
                              (when (:event event)
                                (rf/dispatch [(:event event) (merge event {:request opts} payload)])))]

        (when path
          (swap! db assoc-in (conj path :loading) true)
          (swap! db assoc-in (conj path :status) :loading))

        (-> (js/fetch (str api-base-url "/rpc")
                      (clj->js {:method "post", :mode "cors",
                                :headers {"accept" "application/json", "Cache-Control" "no-cache",
                                          "Content-Type" "application/json"},
                                :cache "no-store",
                                :body (let [payload (select-keys opts [:method :params :id :policy])]
                                        (to-json payload))}
                               :keyword-fn (fn [x] (subs (str x) 1))))
            (.then
             (fn [resp]
               (.then (.text resp)
                      (fn [doc]
                        (let [cdoc (from-json doc)]
                          (if-let [res  (and (< (.-status resp) 299) (or (:result cdoc) (get cdoc "result")))]
                            (do (swap! db update-in path merge {:status :ok :loading false :data res})
                                (when success (dispatch-event success {:response resp :data res})))
                            (do
                              (swap! db update-in path merge {:status :error :loading false :error (or (:error cdoc) cdoc)})
                              (when error
                                (dispatch-event error {:response resp :data (or (:error cdoc) cdoc)})))))))))
            (.catch (fn [err]
                      (.error js/console err)
                      (swap! db update-in path merge {:status :error :loading false :error {:err err}})
                      (when error
                        (dispatch-event error {:error err})))))))))

(defn rpc-fetch [opts]
  (when opts
    (if (vector? opts)
      (doseq [o opts] (when (some? o) (*rpc-fetch o)))
      (*rpc-fetch opts))))

(rf/reg-fx :zen/rpc rpc-fetch)

(defn stop-poll [{id :poll-id}]
  (when-let [abort (:abort (get @abort-state id))]
    #?(:cljs (.abort abort) :clj nil)
    (swap! abort-state dissoc id)))

(defn start-poll [{id :poll-id :as opts}]
  (stop-poll opts)
  (let [abort (abort-ctrl)]
    (fetch (-> opts
               (assoc :signal #?(:cljs (.-signal abort) :clj nil))
               (assoc-in [:params :long-polling] true))
           (fn [{err :error res :result}]
             (when-let [suc (and res (:success opts))]
               (rf/dispatch [(:event suc) (assoc suc :data res)]))
             (when (:txid res)
               (start-poll (assoc-in opts [:params :txid] (:txid res))))
             (when (and err #?(:cljs (not (= (.-name err) "AbortError")) :clj true))
               #?(:cljs (js/setTimeout #(start-poll opts) 1000)))))
    (swap! abort-state assoc id {:abort abort :txid (get-in opts [:params :txid])})))


(rf/reg-fx :zen/rpc-start-poll start-poll)
(rf/reg-fx :zen/rpc-stop-poll stop-poll)
