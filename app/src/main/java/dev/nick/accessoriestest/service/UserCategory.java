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

package dev.nick.accessoriestest.service;

import dev.nick.accessoriestest.player.repository.DatabaseHelper;

public class UserCategory {

    public static final UserCategory FAVOURITE = new UserCategory(DatabaseHelper.LikedColumns.TABLE_NAME);
    public static final UserCategory RECENT = new UserCategory(DatabaseHelper.RecentColumns.TABLE_NAME);
    public static final UserCategory ALL = new UserCategory("ALL");

    private String name;

    public UserCategory(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
