/*
 * Copyright (c) 2018. The Ontario Institute for Cancer Research. All rights reserved.
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

package bio.overture.rollcall.index;

import bio.overture.rollcall.index.ResolvedIndex.Part;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
class IndexNamePart {

  Part part;
  String value;
  List<IndexNamePart> children;

  IndexNamePart(Part part, String value) {
    this.part = part;
    this.value = value;
    this.children = Collections.emptyList();
  }

}
