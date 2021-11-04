(defproject chord-demo "1.0.0-SNAPSHOT"
  :description "Quick demo of how to do chording in Java"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [net.java.dev.jna/jna "5.9.0"]
                 [net.java.dev.jna/jna-platform "5.9.0"]
                 [com.github.kwhat/jnativehook "2.2.0"]
                 [bagotricks "1.5.5"]]
  :main ^:skip-aot chord-demo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
