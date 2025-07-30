(do
  (assert (instance? com.github.ivarref.capturesoutserr.ReplayConsumePrintStream
                     com.github.ivarref.run-server/replay-stream))
  #_(assert (instance? java.io.OutputStreamWriter
                       *out*))
  (.setConsumer ^com.github.ivarref.capturesoutserr.ReplayConsumePrintStream
                com.github.ivarref.run-server/replay-stream
                (reify java.util.function.Consumer
                  (accept [this s]
                    (com.github.ivarref.run-server/debug s))))
  (.println System/out "Hello from System/out")
  (println "Hello from client.clj")

  (alter-var-root #'*out* (fn [_] *out*))
  (let [res @(future (fn []
                       (Thread/sleep 1000)
                       (.println System/out "Hello from background thread on System/out")
                       (println "Hello from background thread on println")
                       :thread-done))]
    #_(com.github.ivarref.run-server/debug (str (class res)))
    [:client-clj-done res])
  #_(alter-var-root #'*out* (fn [_] *out*))

  #_(println (str "*out* is " (.getClass *out*)))
  #_(println (com.github.ivarref.run-server/hook-sout)))
