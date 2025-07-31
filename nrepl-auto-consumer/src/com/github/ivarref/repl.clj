(do
  (import '(com.github.ivarref.capturesoutserr ReplayConsumePrintStream))
  (let [curr-out *out*
        to-client (fn [lin]
                      (binding [*out* curr-out]
                               (println lin)))]
       (assert (instance? ReplayConsumePrintStream System/out))
       (.setConsumer ^ReplayConsumePrintStream System/out to-client)))
