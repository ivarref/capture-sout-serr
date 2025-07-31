(ns com.github.ivarref.repl
  (:require [com.github.ivarref.run-server :as server])
  (:import (com.github.ivarref.capturesoutserr ReplayConsumePrintStream)))

(set! *warn-on-reflection* true)

(binding [*out* *err*]
  (println "starting ...")
  @(promise))
