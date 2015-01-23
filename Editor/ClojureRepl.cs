// -*- c-basic-offset: 2; -*-

using UnityEngine;
using UnityEditor;
using clojure.lang;
using System;
using System.Net;
using System.Text;
using System.Net.Sockets;
using System.Collections.Generic;
using System.Threading;

[InitializeOnLoad]
public class ClojureRepl : EditorWindow {
  static ClojureRepl() {
    // TODO read from config
    RT.load("arcadia/repl");
    RT.load("arcadia/repl/server");
  }

  [MenuItem ("Arcadia/REPL/Window...")]
  public static void Init () {
    ClojureRepl window = (ClojureRepl)EditorWindow.GetWindow (typeof (ClojureRepl));
  }

  public static void Update() {
    RT.var("arcadia.repl", "eval-queue").invoke();
  }

  [MenuItem ("Arcadia/REPL/Start UDP %#r")]
  public static void StartUDP () {
    RT.var("arcadia.repl", "start-server").invoke(11211);
    EditorApplication.update += ClojureRepl.Update;
  }

  [MenuItem ("Arcadia/REPL/Stop UDP &#r")]
  public static void StopUDP () {
    RT.var("arcadia.repl", "stop-server").invoke();
    EditorApplication.update -= ClojureRepl.Update;
  }

  [MenuItem ("Arcadia/REPL/Start TCP %#r")]
  public static void StartTCP () {
    RT.var("arcadia.repl.server", "start").invoke(11212);
    EditorApplication.update += ClojureRepl.Update;
  }

  [MenuItem ("Arcadia/REPL/Stop TCP &#r")]
  public static void StopTCP () {
    RT.var("arcadia.repl.server", "stop").invoke();
    EditorApplication.update -= ClojureRepl.Update;
  }

  void OnInspectorUpdate() {
    // Manually repaint the window with some regularity. If we do not do this
    // then OnGUI() will only be called while the window has focus.
    Repaint();
  }

  void OnGUI()
  {
    // bool running = (bool)RT.var("arcadia.repl", "is-running?").invoke();
    // if (running)
    // {
    //   GUI.color = Color.red;
    //   if (GUILayout.Button("Stop REPL"))
    //   {
    //     ClojureRepl.StopREPL();
    //   }
    // }
    // else
    // {
    //   GUI.color = Color.green;
    //   if (GUILayout.Button("Start REPL"))
    //   {
    //     ClojureRepl.StartREPL();
    //   }
    // }
  }
}