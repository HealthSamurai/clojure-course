(ns clojure-course.activity-tracker.view
  (:require [clojure-course.pages]
            [zf]
            [stylo.core :refer [c]]
            [clojure-course.activity-tracker.model :refer [activity-tracker-s open-activity-tracker activity-tracker-open?
                                                           close-activity-tracker last-action]]
            [clojure-course.course-tree.model :refer [selected-module
                                                      selected-chapter]]))


(defn get-activity-cell-color [activity]
  (cond
    (>= activity 10) (c {:background-color "#067659"})
    (>= activity 3) (c {:background-color "#22D677"})
    (>= activity 1) (c {:background-color "#69FDAF"})
    (>= activity 0) (c {:background-color "#EBEBEB"})))


(zf/defv activity-tracker [activity-tracker-s activity-tracker-open? selected-module selected-chapter last-action]
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
                     :justify-between
                     :flex [:p 17])}

     [:div {:class (c :flex [:space-x 15])}
      [:div {:class (c [:rounded 16] {:background-color "#22D677"}
                       [:py 10] [:px 20]
                       [:w-min "334px"]
                       :flex :flex-col)}
       [:div {:class (c [:text :white] :uppercase :font-bold)} ((fnil name "Module is not selected") selected-module)]
       [:div {:class (c :text-3xl :capitalize {:color "#037659"} :font-bold)} ((fnil name "Chapter is not selected") selected-chapter)]]

      [:div {:class (c :shadow-md :flex [:py 8] [:px 13] [:bg :white] [:rounded 16] :items-center)}
       [:div {:class (c :grid [:grid-rows 5] [:grid-cols 7] [:mr 5])}
        (for [{:as _cell, :keys [activity]} activity-tracker-s]
          [:div {:class [(c [:rounded :full] [:w 6] [:h 6] [:mx 2.5] [:my 1])
                         (get-activity-cell-color activity)]}])]
       [:div {:class (c :flex :flex-col)}
        [:div {:class (c :capitalize :uppercase :font-bold {:color "#3661EB"} [:mb 3])}
         "Last Update"]
        [:div {:class (c [:text :black] :uppercase  :text-3xl [:mb 2])} (:module last-action) ": " (:chapter last-action)]
        [:div {:class (c [:text :black] :text-2xl)} (:test-name last-action)]]]]
     [:div {:class (c :font-bold [:rounded :full] [:w 10] [:h 10] {:background-color "#D9D9D9"} :text-center
                      [:hover :cursor-pointer])
            :on-click close-activity-tracker} "×" ]]))
