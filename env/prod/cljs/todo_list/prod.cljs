(ns todo-list.prod
  (:require [todo-list.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
