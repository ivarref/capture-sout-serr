(ns com.github.ivarref.run-server
  (:require [nrepl.server :as nrepl-server]
            [nrepl.core :as nrepl-client])
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
        (println "Server received exception:" e)))
    (finally
      (println "nREPL server exiting"))))

(defn run-client [_]
  (try
    (println "Starting client server ...")
    #_(System/setOut replay-stream)
    #_(alter-var-root #'*out* (fn [_] (OutputStreamWriter. replay-stream)))
    #_(println "not shown on -X:run-server")
    #_(nrepl-server/start-server :port 7888)
    (nrepl-client/connect :host "127.0.0.1" :port 7888)
    @(promise)
    (catch Exception e
      (binding [*out* *err*]
        (println "Client received exception:" e)))
    (finally
      (println "nREPL client exiting"))))

