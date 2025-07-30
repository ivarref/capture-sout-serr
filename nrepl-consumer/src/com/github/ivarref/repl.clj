(ns com.github.ivarref.repl
  (:import (com.github.ivarref.capturesoutserr ReplayConsumePrintStream)
           (java.util.function Consumer)))

(comment)

(set! *warn-on-reflection* true)

(let [curr-out *out*
      to-client (fn [lin]
                  (binding [*out* curr-out]
                    (println lin)))]
  (assert (instance? ReplayConsumePrintStream System/out))
  (.setConsumer ^ReplayConsumePrintStream
                System/out
                to-client))