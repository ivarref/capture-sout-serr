
(defn has-lines? []
  (if (not (.exists (clojure.java.io/file "./nrepl_client_out.log")))
    false
    (let [content (slurp "./nrepl_client_out.log")
          lines-set (into #{} (clojure.string/split-lines content))]
      (and (contains? lines-set "System/out println from background thread done")
           (contains? lines-set "println from background thread done")))))

(while (not (has-lines?))
  (Thread/sleep 16))

(println "nrepl client is done")