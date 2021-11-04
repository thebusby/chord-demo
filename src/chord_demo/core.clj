(ns chord-demo.core
  (:require [bagotricks :refer [std-keyword re-get to-long fold-into-vec fold-into-lazy-seq dump-lines-to-file read-lines split-tsv fn->> fn-> <- thread okay-string?]])

  (:import (java.awt Robot Rectangle Toolkit))
  (:import (com.sun.jna.platform.win32 WinDef User32))
  (:gen-class))

(defn sleep-ms
  "Puts the current thread to sleep for number of milliseconds provided"
  [sleep-ms]
  (try (Thread/sleep (to-long sleep-ms))
       (catch InterruptedException ie)))


;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ;
;; Handle keyboard events
;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ;

(def event-queue-size
  1000)

(defonce event-queue
  (java.util.concurrent.LinkedBlockingQueue. event-queue-size))

(defn push-event [ev]
  (.put event-queue ev))

(defn take-event []
  (.take event-queue))

(defn get-lazy-queue [^java.util.concurrent.LinkedBlockingQueue q]
  "Return a lazy sequence of objects from the queue provided.
   calls 'take' so removes elements from the queue, and waits if nothing is available"
  (repeatedly #(.take q)))

(defn get-key-text [kc]
  (com.github.kwhat.jnativehook.keyboard.NativeKeyEvent/getKeyText kc))

(def keycode
  {30 \a
   48 \b
   46 \c
   32 \d
   18 \e
   33 \f
   34 \g
   35 \h
   23 \i
   36 \j
   37 \k
   38 \l
   50 \m
   49 \n
   24 \o
   25 \p
   16 \q
   19 \r
   31 \s
   20 \t
   22 \u
   47 \v
   17 \w
   45 \x
   21 \y
   44 \z
   57 \space
   ;; 11 \0
   ;; 2 \1
   ;; 3 \2
   ;; 4 \3
   ;; 5 \4
   ;; 6 \5
   ;; 7 \6
   ;; 8 \7
   ;; 9 \8
   ;; 10 \9
   ;; 28 \newline
   14 \backspace
   })


;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ;
;; Generate keyboard events
;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ;

(defn push-key [key]
  (let [robot   (java.awt.Robot.)]
    (.keyPress robot key)
    (sleep-ms 3)
    (.keyRelease robot key)
    (sleep-ms 2)))

(defn type-word [^String s]
  (doseq [key (some->> s
                       (.toUpperCase)
                       (map int))]
    (push-key key)))

(defn hit-backspace
  ([] (hit-backspace 1))
  ([n] (dotimes [x n]
         (push-key java.awt.event.KeyEvent/VK_BACK_SPACE)
         (sleep-ms 2))))


;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ;
;; Connect to OS Key events
;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ; ;; ;

(defonce native-key-listener
  (reify com.github.kwhat.jnativehook.keyboard.NativeKeyListener
    (nativeKeyPressed [this ev]
      (push-event [ev :pressed (System/nanoTime)]))
    (nativeKeyReleased [this ev]
      (push-event [ev :released (System/nanoTime)]))
    (nativeKeyTyped [this ev]
      ;; (push-event [ev :typed (System/nanoTime)])
      (do)
      )))


(def chord-map
  {#{\b \c} "because "
   #{\t \e} "the "
   #{\a \n} "and "
   })

(def cord-delay-value 60)

(defn do-chording []
  (some->> event-queue
           get-lazy-queue
           (reduce (fn [[agg state ats] [k e ts]]
                     (let [kc (.getKeyCode k)
                           key-char (keycode kc)
                           reset-state [[key-char] e ts]
                           delta (long (/ (- ts ats) 1000000))]

                       ;; Only deal with specific characters
                       (if (not key-char)
                         reset-state

                         (do
                           ;; DEBUG
                           (println (str "GOOD Agg:" agg " " state " " ats " | " key-char " " e " " ts " - D" delta))

                           ;; Initial state
                           (if (nil? state)
                             reset-state

                             ;; Case it's a second key of same state
                             (if (= e state)

                               ;; This is the second event
                               (if (= e :pressed)

                                 ;; This is the second press
                                 (if (< delta
                                        cord-delay-value)
                                   [(conj agg key-char) state ts]

                                   ;; Not pressed fast enough, reset state
                                   reset-state)

                                 ;; This is the second release
                                 (if (< delta
                                        cord-delay-value)
                                   (if-let [word (chord-map (into #{} agg))]
                                     (do
                                       (hit-backspace (count agg))
                                       (type-word word)
                                       [[] e ts])
                                     reset-state)

                                   ;; Not released fast enough, reset state
                                   reset-state))

                               [agg e ts])
                             )))))
                   [[] nil 0])))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (do
    (com.github.kwhat.jnativehook.GlobalScreen/registerNativeHook)
    (com.github.kwhat.jnativehook.GlobalScreen/addNativeKeyListener native-key-listener)
    (try (do-chording)
         (catch Exception _))
    (com.github.kwhat.jnativehook.GlobalScreen/removeNativeKeyListener native-key-listener)
    (com.github.kwhat.jnativehook.GlobalScreen/unregisterNativeHook)
    ))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment











  )
