(ns clojure-course.index
  (:require [re-frame.core :as rf]
            [clojure-course.pages]
            [clojure-course.interop]
            [clojure-course.routes]
            #?(:cljs [reagent.dom])


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
            [zframes.dispatch-when]
            [stylo.core :refer [c]]
            [clojure-course.course-tree.view]))

(defn current-page []
  (let [{page :match params :params :as obj} @(rf/subscribe [:route-map/current-route])
        route-error @(rf/subscribe [:route-map/error])
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
    [:main {:class (c {:background-color "white"} :h-screen :w-auto
                      :flex
                      [:mx 65] [:my 50] [:rounded 42]
                      :overflow-hidden)}
     [:div {:class (c [:w "25%"] {:background-color "#F8F8F8"})}
      [clojure-course.course-tree.view/course-tree]]
     [:div {:class (c [:w "75%"])}]]))


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
                 :path [::initialize]}})))


(defn init! []
  (rf/dispatch [::initialize])
  (mount-root))
