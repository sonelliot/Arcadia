(ns squiggle
  (:use unity.core)
  (:import [UnityEngine LineRenderer Vector3 Quaternion Color Debug]))

(defn set-line [^LineRenderer lr vs]
  (.SetVertexCount lr (count vs))
  (loop [i 0]
    (if (< i (count vs))
      (do 
        (.SetPosition lr i (nth vs i))
        (recur (inc i))))))

(defn next-squig-point [v1 speed]
  (let [v1 (Vector3/op_Addition v1
             (Vector3/op_Multiply speed UnityEngine.Random/insideUnitSphere))]
    (Vector3/op_Multiply 0.99 v1)))

(defcomponent Squiggle [trails
                        ^int size
                        ^float speed]
  (Awake [this] 
    (require 'squiggle))

  (Start [this]
    (let [r (rand)
          g (rand)
          b (rand)]
      (.. this (GetComponent LineRenderer) (SetColors
                                             (Color. r g b 0)
                                             (Color. r g b))))
    (set! trails
      (->> Vector3/zero
        (iterate #(next-squig-point % speed))
        (take size)
        vec)))
  
  (Update [this]
    (set-line
      (.. this (GetComponent LineRenderer))
      (CatmullRomSpline/Generate
        (into-array Vector3 trails)
        15))
    (set! trails
      (conj (subvec trails 1)
        (next-squig-point (peek trails) speed)))))

(defcomponent LookAtSquiggle [^Squiggle squiggle ^float follow]
  (Awake [this]
     (require 'squiggle))
  (Update [this]
    (let [trails  (.. squiggle trails)
          focus   (ffirst trails)
          pos     (.. this transform position)
          dir     (Vector3/op_Subtraction focus pos)
          new-dir (Vector3/RotateTowards (.. this transform forward) dir (* follow Time/deltaTime) 0)
          rot     (Quaternion/LookRotation new-dir)]
      (set! (.. this transform position)
            (Vector3/MoveTowards
              pos focus (* follow Time/deltaTime)))
      (set! (.. this transform rotation)
            rot))))

(defn clone-squiggle []
  (let [^GameObject s (GameObject/Find "Squiggle")]
    (GameObject/Instantiate s)))
