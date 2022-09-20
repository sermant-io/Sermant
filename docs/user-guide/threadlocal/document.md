# Threadlocal

[简体中文](document-zh.md) | [English](document.md) 

This document focuses on the [Threadlocal Plugin](../../../sermant-plugins/sermant-threadlocal) and how to use it.

## Function
Threadlocal plugin is mainly used to transfer threadlocals between threads. When a thread submits a task to the thread pool, it is guaranteed to refresh the latest thread variable to the specific thread executing the task each time it submits a task.

## Instruction
The plugin works out of the box and does not require any further modifications.

[[Back to README of **Sermant**](../README.md)](../../README.md)
