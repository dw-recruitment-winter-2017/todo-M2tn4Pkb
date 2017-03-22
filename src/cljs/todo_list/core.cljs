(ns todo-list.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))
              
(defonce todos (reagent/atom (sorted-map)))
(defonce counter (reagent/atom 0))

;;Todo item functions
(defn add [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :title text :done false})))
(defn save [id title] 
  (swap! todos assoc-in [id :title] title))
(defn delete [id] 
  (swap! todos dissoc id))
(defn toggle [id] 
  (swap! todos update-in [id :done] not))

(defn todoInput [{:keys [title on-save on-stop]}]
  (let [val (reagent/atom title)
    stop #(do (reset! val "")
      (if on-stop (on-stop)))
    save #(let [v (-> @val str clojure.string/trim)]
      (if-not (empty? v) (on-save v))
      (stop))]
    (fn [{:keys [id class]}]
      [:input {:type "text"
               :placeholder "Add a new todo item" 
               :value @val
               :id id 
               :class class 
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))
(defn todoItem []
  (let [showingMenu (reagent/atom false)] 
  (fn [{:keys [id done title]}]
      [:li {:class (str (if done "completed "))}
        [:div#todoItem {:on-double-click #(reset! showingMenu true)}
          [:input.toggle {:type "checkbox" 
                          :checked done 
                          :on-change #(toggle id)}]
          [:div.todoDesc title]
          (when @showingMenu 
            [:div#todoMenu {:on-click #(reset! showingMenu false)}
              [:ul
                [:li [:span {:on-click #(delete id)} "Delete todo"]]]])]])))

(defn todolist []
(fn []
    (let [items (vals @todos)
          done (->> items (filter :done) count)
          active (- (count items) done)]
      [:div 
        [:div 
          [:h1 "Todo List"] 
            [:div 
              [:a {:href "/about"} "About"]]]                                      
        [:div.todos
            [:div#addTodos
              [todoInput {:id "todoInput"
                          :on-save add}]]    
            (when (-> items count pos?)
              [:div#activeTodoList
                [:ul#todo-list
                  (for [todo (filter (complement :done) items)]
                    ^{:key (:id todo)} [todoItem todo])]])
            (when (-> (filter :done items) count pos?)                
              [:div#compTodoList 
                [:h2 "Completed Todos"]
                [:ul
                  (for [todo (filter :done items)]
                    ^{:key (:id todo)} [todoItem todo])]])]]
                    )))
;;Render pages
(defn home-page []
  (todolist))
(defn about-page []
  [:div.topMenu [:h1 "About"]
   [:div [:a {:href "/"} "Todo List"]]
   [:p "The Todo List app allows you to add, delete and mark complete TODO items. "]
   [:p "Features:"]
   [:ol
    [:li "Add a new TODO (initially incomplete)"]
    [:li "Mark a TODO as completed"]
    [:li "Unmark a TODO as completed (i.e. return it to incomplete state)"]
    [:li "Delete existing TODOs"]] 
    [:p "Usage:"]
    [:ol 
      [:li [:b "Add"] " -  Type in a description in the input text field and click enter."]
      [:li [:b "Complete"] " - Click on the checkbox next to the TODO item."]
      [:li [:b "Delete"] " - Double click on a TODO item to get an options window. Click on the 'Delete todo' link to delete or click on the window to close."]
      ]])

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
