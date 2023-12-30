/*
 *  Copyright 2017 Eclipse HttpClient (http4e) http://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.core.client.model;

import java.io.Serializable;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public interface Model {

    void addListener(ModelListener listener);

    void removeListener(ModelListener listener);

    void fireExecute(ModelEvent e);

    Serializable getSerializable();

    void load(Serializable serializable);
}
