/* 
 * Copyright 2016 Kenneth Verheggen <kenneth.verheggen@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.compomicscrowd.control.provider;

import com.compomics.compomicscrowd.model.denovo.DeNovoTask;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public interface DeNovoTaskProvider {

    /**
     * Gets the next DeNovoTask
     *
     * @return a denovo task
     */
    public DeNovoTask getNextTask();

}
