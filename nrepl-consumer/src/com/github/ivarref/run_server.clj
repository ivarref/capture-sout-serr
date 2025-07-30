(ns com.github.ivarref.run-server
  (:require [nrepl.server :as nrepl-server])
  (:import (com.github.ivarref.capturesoutserr ReplayConsumePrintStream)
           (java.io OutputStreamWriter)))

(defonce original-stdout System/out)

(defonce replay-stream (ReplayConsumePrintStream.))

(defn run-server [_]
  (try
    (println "Starting nrepl server ...")
    (System/setOut replay-stream)
    (alter-var-root #'*out* (fn [_] (OutputStreamWriter. replay-stream)))
    (println "not shown on -X:run-server")
    (nrepl-server/start-server :port 7888)
    @(promise)
    (catch Exception e
      (binding [*out* *err*]
        (println "Received exception:" e)))
    (finally
      (println "nREPL server exiting"))))
