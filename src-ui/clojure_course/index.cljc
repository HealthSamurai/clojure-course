(ns clojure-course.index
  (:require [re-frame.core :as rf]
            [clojure-course.pages]
            [clojure-course.interop]
            [clojure-course.routes]
            #?(:cljs [reagent.dom])

            [zf]
            #_"zframes"
            [zframes.cookies :as cookies]
            #?(:cljs [zframes.routing])
            #?(:cljs [zframes.redirect])
            #?(:cljs [zframes.xhr])
            #?(:cljs [zframes.rpc])
            #?(:cljs [zframes.debounce])
            #?(:cljs [zframes.storage :as storage])
            #?(:cljs [zframes.redirect :as redirect])
            #?(:cljs [zframes.hotkeys :as hotkeys])
            #?(:cljs [zframes.window-location :as location])
            [zframes.ws]
            [zframes.dispatch-when]
            [stylo.core :refer [c]]
            [clojure-course.course-tree.view]
            [clojure-course.activity-tracker.view]
            [clojure-course.activity-tracker.model :refer [activity-tracker-open? ]]))

(defn current-page []
  (let [{page :match params :params :as obj} @(rf/subscribe [:route-map/current-route])
        route-error @(rf/subscribe [:route-map/error])
        activity-tracker-open? @(rf/subscribe [:clojure-course.activity-tracker.model/activity-tracker-open?])
        params (assoc params
                 :route page
                 :route-ns (when page (namespace page)))
        content (if page
                  (if-let [cmp (get @clojure-course.pages/pages page)]
                    [cmp params]

                    [:div.not-found (str "Page not found [" (str page) "]")])
                  (case route-error
                    nil [:div]
                    :not-found [:div.not-found (str "Route not found ")]
                    [:div.not-found (str "Routing error")]))]
    [:main {:class (c [:mx 65] [:my 50])}
     [clojure-course.activity-tracker.view/activity-tracker]
     [:div {:class [(when-not activity-tracker-open?
                      (c [:rounded-t 42]))
                    (c {:background-color "white"} :h-screen :w-auto :flex
                       [:rounded-b 42]
                       :overflow-hidden)]}
      [:div {:class (c [:w "30%"] [:px 20] [:py 15] {:background-color "#F8F8F8"})}
       [clojure-course.course-tree.view/course-tree]]
      [:div {:class (c [:w "70%"])}]]]))


(zf/defx ws-receive [{db :db} data]
  (println "ws-receive" data)
  {:db (assoc-in db [:clojure-course.course-tree.model/index :data] (get-in data [:data :result]))})

(zf/defx ws-reopen [{db :db} data]
  (println "ws-reopen")
  (println data))

(zf/defx ws-open [{db :db} data]
  (println "ws-open")
  (println data))


(defn mount-root []
  #?(:clj  #()
     :cljs (reagent.dom/render
             [current-page]
             (.getElementById js/document "root"))))


(rf/reg-event-fx
  ::initialize
  (fn [{db :db} _]
    (let [location-hash (clojure-course.interop/get-location-hash)]
      {:db {:route-map/routes clojure-course.routes/routes}
       :route-map/start {}
       :zen/rpc {:method 'rpc-ops/get-course-tree
                 :path [::initialize]}
       :ws/connect {:id :course-tree/ws
                    :uri "/ws"
                    :open {:event ::ws-open}
                    :reopen {:event ::ws-reopen}
                    :receive {:event ::ws-receive}}})))

(defn init! []
  (rf/dispatch [::initialize])
  (mount-root))
