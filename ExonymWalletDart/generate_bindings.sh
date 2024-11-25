#!/bin/bash
pico ./../libexonymwallet/target/gluonfx/aarch64-darwin/gvm/libexonymwallet/io.exonym.lib.wallet.walletapi.h
dart run ffigen --config config.yaml
