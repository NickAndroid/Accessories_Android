/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.accessoriestest.media;

public class Test {
    Class clz;
    String title;
    String description;

    public Test(Class clz, String title, String description) {
        this.clz = clz;
        this.title = title;
        this.description = description;
    }

    public Test(Class clz, String description) {
        this.clz = clz;
        this.description = description;
    }

    public Test(Class clz) {
        this.clz = clz;
        this.title = clz.getSimpleName();
        this.description = "No description found for this test_movie";
    }
}
