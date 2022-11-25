(ns zframes.ws
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [zf :as zrf]
            [clojure.string :as str]))

(def parse
  #?(:clj identity
     :cljs (fn [s] (-> (.parse js/JSON s)
                       (js->clj :keywordize-keys true)))))


(def unparse
  #?(:clj identity
     :cljs (fn [s] (->> (clj->js s)
                        (.stringify js/JSON)))))


(zrf/defx set-state
  [{db :db} id state]
  {:db (assoc-in db [::db id :state] state)})


(zrf/defx add-conn
  [{db :db} id ws]
  {:db (update-in db [::db id]
                  (fn [m] (-> m
                              (assoc :ws ws)
                              (update :state #(or % :created)))))})


(zrf/defe :ws/close-conn [conn]
  (.close conn))

(zrf/defx remove-conn
  [{db :db} id]
  (let [conn (get-in db [::db id :ws])]
    {:db (update db ::db dissoc id)
     :ws/close-conn conn}))


(zrf/defx add-on-open
  [{db :db} id event]
  {:db (update-in db [::db id :op-open] (fnil conj []) event)})


(zrf/defx dispatch-on-open
  [{db :db} id]
  {:db (assoc-in db [::db id :on-open] nil)
   :dispatch-n (or (get-in db [::db id :on-open]) [])})


(zrf/defx add-message
  [{db :db} message-id success error one-off?]
  {:db (assoc-in db [::db :messages message-id] {:success success :error error :one-off? one-off?})})


(zrf/defx remove-message
  [{db :db} message-id]
  {:db (update-in db [::db :messages] dissoc message-id)})



#?(:cljs
   (do
     (zrf/defe :ws/connect
       [{:keys [id uri open reopen receive] :as options}]
       (let [label (str id " [" (gensym "") "]")
             ws (new js/WebSocket
                     (str
                       (if (= "https:" js/window.location.protocol)
                         "wss://" "ws://")
                       js/window.location.host
                       uri))]

         (zrf/dispatch [::add-conn id ws])

         (set! (.-onopen ws)
               (fn []
                 (js/console.log (str "Websocket " label " opened."))
                 (let [prev-state (get-in @app-db [::db id :state])]
                   (zrf/dispatch [::set-state id :opened])
                   (if (= :created prev-state)
                     (do (when-let [event (:event open)]
                           (zrf/dispatch [event open]))
                         (zrf/dispatch [::dispatch-on-open id]))
                     (when-let [event (:event reopen)]
                       (zrf/dispatch [event reopen]))))))

         (set! (.-onmessage ws)
               (fn [e]
                 (let [data (parse (.-data e))]
                   (when-let [event (:event receive)]
                     (zrf/dispatch [event (assoc receive :data data)]))
                   (when-let [{:keys [success error one-off?]} (get-in @app-db [::db :messages (:id data)])]
                     (when (and success (:result data))
                       (zrf/dispatch [(:event success) (merge success data)]))
                     (when (and error (:error data))
                       (zrf/dispatch [(:event error) (merge error data)]))
                     (when one-off?
                       (zrf/dispatch [remove-message (:id data)]))))))

         (set! (.-onerror ws)
               (fn [e]
                 (js/console.log (str "Websocket " label " encountered error: " (.-message ^js e)))
                 (.close ws)))

         (set! (.-onclose ws)
               (fn [_]
                 (when (= ws (get-in @app-db [::db id :ws]))
                   (zrf/dispatch [set-state id :closed])
                   (js/console.log (str "Will reconnect in 1 second."))
                   (js/setTimeout #(zrf/dispatch [:ws/connect options]) 1000))))))


     (zrf/defe :ws/disconnect
       [{:keys [id]}]
       (zrf/dispatch [remove-conn id]))


     (zrf/defe :ws/send
       [{:keys [id data success error one-off?] :or {one-off? true}}]
       (if-let [conn (get-in @app-db [::db id :ws])]
         (let [message-id (or (:id data)
                              (when (or success error)
                                (random-uuid)))]
           (.send conn (unparse (cond-> data message-id (assoc :id message-id))))
           (when message-id
             (zrf/dispatch [add-message message-id success error one-off?])))
         (js/console.warn "Cannot send when disconnected")))))


(zrf/defe :ws/on-open
  [{:keys [id event]}]
  (if (= :opened (get-in @app-db [::db id :state]))
    (do
      (println "on-open")
      (rf/dispatch event))
    (swap! app-db update-in [::db id :on-open] (fnil conj []) event)))
