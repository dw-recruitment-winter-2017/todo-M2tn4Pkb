(ns todo-list.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

(defonce currentpage (reagent/atom ""))
(defonce todos (reagent/atom (sorted-map)))
(defonce counter (reagent/atom 0))
;; -------------------------
;; Views

;;top menu
(defn home-page []
  [:div [:h2 "Todo List"]
   [:div [:a {:href "/about"} "About"]]])

(defn about-page []
  [:div [:h2 "About"]
   [:div [:a {:href "/"} "Todo List"]]
   [:p "The Todo List app allows you to add, delete and mark complete todo items."] 
    [:p "Instructions:"]
    [:ol 
      [:li [:b "Add"] " -  Type in a description in the input text field and click enter"]
      [:li [:b "Delete"] " - Double click on a todo item. Click on the 'Delete todo' link"]
      [:li [:b "Complete"] " - Click on the checkbox next to the todo item"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
