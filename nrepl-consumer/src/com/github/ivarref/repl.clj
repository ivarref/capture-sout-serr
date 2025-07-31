(ns com.github.ivarref.repl
  (:require [com.github.ivarref.run-server :as server])
  (:import (com.github.ivarref.capturesoutserr ReplayConsumePrintStream)))

(set! *warn-on-reflection* true)

(defonce seen-lines (atom #{}))
(defonce received-all-lines? (promise))

(let [curr-out *out*
      to-client (fn [lin]
                  (binding [*out* curr-out]
                    (swap! seen-lines conj lin)
                    (println "RECEIVED:" lin)
                    (when (and
                            (contains? @seen-lines "println from background thread done")
                            (contains? @seen-lines "System/out println from background thread done"))
                      (println "client received all lines!")
                      (deliver received-all-lines? true))))]
  (assert (instance? ReplayConsumePrintStream System/out))
  (.setConsumer ^ReplayConsumePrintStream
                System/out
                to-client))

(do
  @received-all-lines?
  (deliver server/client-done? true))
