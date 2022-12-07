(ns clojure-course.course-content.view
  (:require [clojure-course.pages]
            [clojure-course.course-content.model :refer [selected-content]]
            [clojure-course.content]
            [stylo.core]
            [zf]
            #?(:cljs [react :as react])))


(def shadow-host-styles-css
  ":host {
     all: initial;
     display: block;
     contain: content;
  }")


(zf/defv course-content [selected-content]
  ;; Shadow here refers to Shadow DOM, not Shadow CLJS
  #?(:cljs (let [shadow-host-ref (react/useRef nil)]
             ;; Initialize shadow root on the first render
             (react/useEffect (fn []
                                (prn (:stylo-styles selected-content))
                                (when-let [content-shadow-root (some-> shadow-host-ref
                                                                       .-current
                                                                       (.attachShadow (js-obj "mode" "open")))]
                                  (let [shadow-host-styles-el (.createElement js/document "style")
                                        _ (set! (.-textContent shadow-host-styles-el)
                                                shadow-host-styles-css)

                                        stylo-styles-el (.createElement js/document "style")
                                        _ (.add (.-classList stylo-styles-el)
                                                "stylo-styles")
                                        _ (set! (.-textContent stylo-styles-el)
                                                (:stylo-styles selected-content))

                                        base-styles-el (.createElement js/document "style")
                                        _ (set! (.-textContent base-styles-el)
                                                clojure-course.content/base-styles-css)

                                        font-awesome-link-el (.createElement js/document "link")
                                        _ (set! (.-rel font-awesome-link-el)
                                                "stylesheet")
                                        _ (set! (.-href font-awesome-link-el)
                                                clojure-course.content/font-awesome-css-url)

                                        highlightjs-link-el (.createElement js/document "link")
                                        _ (set! (.-rel highlightjs-link-el)
                                                "stylesheet")
                                        _ (set! (.-href highlightjs-link-el)
                                                clojure-course.content/highlightjs-css-url)

                                        highlightjs-script-main-el (.createElement js/document "script")
                                        _ (set! (.-src highlightjs-script-main-el)
                                                clojure-course.content/highlightjs-js-main-url)
                                        _ (set! (.-async highlightjs-script-main-el) false)

                                        highlightjs-script-clojure-el (.createElement js/document "script")
                                        _ (set! (.-src highlightjs-script-clojure-el)
                                                clojure-course.content/highlightjs-js-clojure-url)
                                        _ (set! (.-async highlightjs-script-clojure-el) false)

                                        content-root-el (.createElement js/document "article")
                                        _ (.add (.-classList content-root-el)
                                                clojure-course.content/content-root-class)]
                                    (doseq [el [shadow-host-styles-el
                                                stylo-styles-el
                                                base-styles-el
                                                font-awesome-link-el
                                                highlightjs-link-el
                                                highlightjs-script-main-el
                                                highlightjs-script-clojure-el
                                                content-root-el]]
                                      (.appendChild content-shadow-root el)))))
                              (array))
             ;; Update content and styles when selected content changes
             (react/useEffect (fn []
                                (when-let [shadow-root (some-> shadow-host-ref
                                                               .-current
                                                               .-shadowRoot)]
                                  (let [content-node (.querySelector shadow-root
                                                                     (str "." clojure-course.content/content-root-class))
                                        stylo-styles-node (.querySelector shadow-root ".stylo-styles")]
                                    (set! (.-innerHTML content-node) (:html selected-content))
                                    (set! (.-innerHTML stylo-styles-node) (:stylo-styles selected-content)))))
                              (array selected-content))
             [:div {:ref shadow-host-ref}])))


(clojure-course.pages/reg-page clojure-course.course-content.model/page course-content)
