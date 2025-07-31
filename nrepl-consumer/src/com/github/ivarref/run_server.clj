(ns com.github.ivarref.run-server
  (:require [clojure.java.io :as io]
            [nrepl.server :as nrepl-server])
  (:import (com.github.ivarref.capturesoutserr ReplayConsumePrintStream
                                               SomeClassThatPrintsAsPartOfInitialization)
           (java.io OutputStreamWriter)
           (java.util.concurrent CountDownLatch)))

(set! *warn-on-reflection* true)

(defonce debug-write-lock (Object.))

(defonce client-done? (promise))

(defn debug [msg]
  (locking debug-write-lock
    (spit "debug.log" (str msg "\n") :append true)))

(defn run-server [_]
  (try
    (println "Starting nREPL server ...")
    (let [replay-stream (ReplayConsumePrintStream. System/out)]
      (System/setOut replay-stream)
      (alter-var-root #'*out* (fn [_] (OutputStreamWriter. replay-stream))))
    (println "not shown on -X:run-server, but will be buffered")
    (SomeClassThatPrintsAsPartOfInitialization.)
    (with-open [server (nrepl-server/start-server :port 7888)]
      (let [latch (CountDownLatch. 2)]
        (future
          (loop [i 1]
            (println (str "println from background thread. Count: " i))
            (if (not= i 3)
              (do
                (Thread/sleep 3000)
                (recur (inc i)))
              (do
                (println "println from background thread done")
                (.countDown latch)))))
        (future
          (loop [i 1]
            (.println System/out (str "System/out println from background thread. Count: " i))
            (if (not= i 3)
              (do
                (Thread/sleep 3000)
                (recur (inc i)))
              (do
                (.println System/out (str "System/out println from background thread done"))
                (.countDown latch)))))
        (.await latch)
        (debug "latch released")
        @client-done?
        (debug "client marked as done")
        (while (not (.exists (io/file "./.nrepl_client_done")))
          (Thread/sleep 16))
        (debug "client exited")))
    (catch Exception e
      (binding [*out* *err*]
        (println "Server received exception:" e)))
    (finally
      (binding [*out* *err*]
        (println "nREPL server exiting")))))
