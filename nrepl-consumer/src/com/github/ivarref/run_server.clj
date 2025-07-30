(ns com.github.ivarref.run-server
  (:require [nrepl.core :as nrepl]
            [nrepl.server :as nrepl-server]
            [nrepl.core :as nrepl-client])
  (:import (com.github.ivarref.capturesoutserr ReplayConsumePrintStream)
           (java.io OutputStreamWriter)))

(defonce original-stdout System/out)

(defonce replay-stream (ReplayConsumePrintStream.))

(defn run-server [_]
  (try
    (println "Starting nREPL server ...")
    #_(System/setOut replay-stream)
    #_(alter-var-root #'*out* (fn [_] (OutputStreamWriter. replay-stream)))
    (println "not shown on -X:run-server, but will be buffered")
    (nrepl-server/start-server :port 7888)
    @(promise)
    (catch Exception e
      (binding [*out* *err*]
        (println "Server received exception:" e)))
    (finally
      (println "nREPL server exiting"))))

(defn debug [msg]
  (spit "debug.log" (str msg "\n") :append true))

(defn hook-sout []
  (debug (pr-str *out*))
  :hook-sout-ok)

(defn run-client [_]
  (try
    (println "Starting nREPL client ...")
    #_(System/setOut replay-stream)
    #_(alter-var-root #'*out* (fn [_] (OutputStreamWriter. replay-stream)))
    #_(println "not shown on -X:run-server")
    #_(nrepl-server/start-server :port 7888)
    (with-open [conn (nrepl/connect :host "127.0.0.1" :port 7888)]
      (-> (nrepl/client conn 3000)    ; message receive timeout required
          (nrepl/message {:op "eval" :code "(com.github.ivarref.run-server/hook-sout)"})
          nrepl/response-values
          (println)))
    (println "Done")
    (catch Exception e
      (binding [*out* *err*]
        (println "Client received exception:" e)))
    (finally
      (println "nREPL client exiting"))))

