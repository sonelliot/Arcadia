(ns squiggle
  (:use unity.core)
  (:import [UnityEngine LineRenderer Vector3 Quaternion Color Debug]))

(defn set-line [^LineRenderer lr vs]
  (.SetVertexCount lr (count vs))
  (loop [i 0]
    (if (< i (count vs))
      (do 
        (.SetPosition lr i (nth vs i))
        (recur (inc i)))))
  (comment (doseq [[^int i ^Vector3 v] (map vector (range) vs)]
    (.SetPosition lr i v))))

(defcomponent Squiggle [trails ^int size]
  (Awake [this]
    (require 'squiggle))

  (Start [this]
    (set! trails (atom (map vec (partition size 1 (iterate
                  #(Vector3/op_Addition % UnityEngine.Random/insideUnitSphere)
                  Vector3/zero))))))
  
  (Update [this]
    (set-line
      (.. this (GetComponent LineRenderer))
      (CatmullRomSpline/Generate
        (into-array Vector3 (first @trails))
        5)) ; (catmul-rom (first @trails) 5)
    (swap! trails next)))

(defcomponent LookAtSquiggle [^Squiggle squiggle ^float follow]
  (Awake [this]
     (require 'squiggle))
  (Update [this]
    (let [trails (.. squiggle trails)
          focus (first (first @trails))
          pos (.. this transform position)
          dir (Vector3/op_Subtraction focus pos)
          new-dir (Vector3/RotateTowards (.. this transform forward) dir (* follow Time/deltaTime) 0)
          rot (Quaternion/LookRotation new-dir)]
      (set! (.. this transform position)
            (Vector3/MoveTowards pos focus (* follow Time/deltaTime)))
      (set! (.. this transform rotation)
            rot))))

(defn clone-squiggle []
  (let [^GameObject s (GameObject/Find "Squiggle")
        news (GameObject/Instantiate s)]
        (set! (.. news (GetComponent LineRenderer) material color)
              (Color. (rand) (rand) (rand)))))


(comment (defn catmul-rom* [^Vector3 p0
                   ^Vector3 p1
                   ^Vector3 p2
                   ^Vector3 p3
                   t]
  (let [t  (float t)
        t0 (float (* (- (* (+ (- t) 2) t) 1) t 0.5))
        t1 (float (* (+ (* (* (- (* 3 t) 5) t) t) 2) 0.5))
        t2 (float (* (+ (* (+ (* -3 t) 4) t) 1) t 0.5))
        t3 (float (* (- t 1) t t 0.5))]
  (Vector3.
    (+ (* t0 (.x p0))
       (* t1 (.x p1))
       (* t2 (.x p2))
       (* t3 (.x p3)))
    (+ (* t0 (.y p0))
       (* t1 (.y p1))
       (* t2 (.y p2))
       (* t3 (.y p3)))
    (+ (* t0 (.z p0))
       (* t1 (.z p1))
       (* t2 (.z p2))
       (* t3 (.z p3))))))

(defn catmul-rom
  ([vs] (catmul-rom vs 20))
  ([vs samples]
    (for [n (range 1 (- (count vs) 2))
          i (range samples)]
      (catmul-rom* 
        (vs (- n 1))
        (vs n)
        (vs (+ n 1))
        (vs (+ n 2))
        (* i (/ 1.0 samples)))))))