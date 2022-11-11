(ns clojure-course.activity-tracker.view
  (:require [clojure-course.pages]
            [zf]
            [stylo.core :refer [c]]
            [clojure-course.activity-tracker.model :refer [activity-tracker-s open-activity-tracker activity-tracker-open?]]))


(defn get-activity-cell-color [activity]
  (cond
    (>= activity 10) (c {:background-color "#067659"})
    (>= activity 3) (c {:background-color "#22D677"})
    (>= activity 1) (c {:background-color "#69FDAF"})
    (>= activity 0) (c {:background-color "#EBEBEB"})))


(zf/defv activity-tracker [activity-tracker-s activity-tracker-open?]
  (if-not activity-tracker-open?
    [:div {:class (c :flex :justify-end [:mx 20] [:my 10])}
     [:div {:class (c :flex :items-center [:hover :cursor-pointer])
            :on-click open-activity-tracker}
      [:div {:class (c [:mr 2.5])}
       [:img {:src "/static/images/dashboard-open.svg"
              :alt "dashboard open"}]]
      [:div {:class (c :text-2xl :font-bold)}

       "Dashboard"]]]
    [:div {:class (c [:rounded-t 42] {:background-color "rgba(255,255,255,0.5)"}
                     [:space-x 15]
                     :flex [:p 17])}
     [:div {:class (c [:rounded 16] {:background-color "#22D677"}
                      [:py 10] [:px 20]
                      :flex :flex-col)}
      [:div {:class (c [:text :white] :uppercase)} "current module"]
      [:div {:class (c :text-3xl :capitalize {:color "#037659"})} "current chapter"]]
     [:div {:class (c :shadow-md :flex [:py 8] [:px 13] [:bg :white] [:rounded 16] :items-center)}
      [:div {:class (c :grid [:grid-rows 5] [:grid-cols 7] [:mr 5])}
       (for [{:as _cell, :keys [activity]} activity-tracker-s]
         [:div {:class [(c [:rounded :full] [:w 6] [:h 6] [:mx 2.5] [:my 1])
                        (get-activity-cell-color activity)]}])]
      [:div {:class (c :flex :flex-col)}
       [:div {:class (c :capitalize :uppercase :font-bold {:color "#3661EB"} [:mb 3])}
                               "Last Update"]
       [:div {:class (c [:text :black] :text-2xl)} "Last Chapter: Last Test"]]]


     ]))