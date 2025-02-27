// Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

val kotlinVersion: String by project
val detektVersion: String by project
val junitVersion: String by project
val assertjVersion: String by project

plugins {
    id("toolkit-kotlin-conventions")
    id("toolkit-testing")
}

dependencies {
    compileOnly(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.junit4)
    testImplementation(libs.assertj)
}
