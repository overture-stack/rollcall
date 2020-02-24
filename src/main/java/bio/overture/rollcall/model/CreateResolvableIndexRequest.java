/*
 * Copyright (c) 2020. The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package bio.overture.rollcall.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class CreateResolvableIndexRequest {

    private final String entity;
    private final String type;
    private final String shardPrefix;
    private final String shard;
    private final String releasePrefix;
    private final Boolean clone;
    private final String indexSetting;

    public CreateResolvableIndexRequest(
            @NonNull String entity,
            @NonNull String type,
            @NonNull String shardPrefix,
            @NonNull String shard,
            String releasePrefix,
            Boolean clone,
            String indexSetting) {
        this.entity = entity;
        this.type = type;
        this.shardPrefix = shardPrefix;
        this.shard = shard;
        this.releasePrefix = releasePrefix == null ? "re" : releasePrefix;
        this.clone = clone == null ? false : clone;
        this.indexSetting = indexSetting == null ? "{}" : indexSetting;
    }
}
