/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
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
package me.glindholm.plugin.http4e2.httpclient.core.client.view.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class Tracker {

    // -------------------
    public interface BlacklistStrategy {
        boolean isBlacklisted(String word);
    }
    // -------------------

    public final static String MASTER_ID = null;

    private final int maxQueueSize;
    private final List<String> keyBuffer;
    private final Collection<String> knownKeys = new HashSet<>();
    private String id = null;
    // String(key), Tracker(values-for-key)
    private final Map<String, Tracker> childTrackers = new HashMap<>();
    private BlacklistStrategy blacklistStrategy = word -> false;

    public Tracker(final String id, final int queueSize) {
        this.id = id;
        maxQueueSize = queueSize;
        keyBuffer = new LinkedList<>();
    }

    public BlacklistStrategy getBlacklistStrategy() {
        return blacklistStrategy;
    }

    public void setBlacklistStrategy(final BlacklistStrategy blacklistStrategy) {
        this.blacklistStrategy = blacklistStrategy;
    }

    public Tracker getChildTracker(final String key) {
        if (key == null || "".equals(key.trim())) {
            throw new IllegalArgumentException("Key is empty");
        }
        Tracker vals = childTrackers.get(key);
        if (vals == null) {
            vals = new Tracker(key, CoreConstants.MAX_TRACKS_VALUES);
            childTrackers.put(key, vals);
        }
        return vals;
    }

    public Collection<Tracker> getChildTrackers() {
        final List<Tracker> kids = new ArrayList<>();
        for (final Iterator<String> iter = childTrackers.keySet().iterator(); iter.hasNext();) {
            final String key = iter.next();
            kids.add(childTrackers.get(key));
        }
        return kids;
    }

    /**
     *
     * @return true if this tracker is value tracker, otherwise is key tracker
     */
    public boolean isMasterTracker() {
        return id == MASTER_ID;
    }

    public int getWordCount() {
        return keyBuffer.size();
    }

    public void add(final String word) {
        if (wordIsNotKnown(word) && !getBlacklistStrategy().isBlacklisted(word)) {
            flushOldestWord();
            insertNewWord(word);
        }
    }

    private void insertNewWord(final String word) {
        keyBuffer.add(0, word);
        knownKeys.add(word);
    }

    private void flushOldestWord() {
        if (keyBuffer.size() == maxQueueSize) {
            final String removedWord = keyBuffer.remove(maxQueueSize - 1);
            knownKeys.remove(removedWord);
        }
    }

    private boolean wordIsNotKnown(final String word) {
        return !knownKeys.contains(word);
    }

    public List<String> suggest(final String word) {
        final List<String> suggestions = new LinkedList<>();
        String currWord;
        for (final Iterator<String> i = keyBuffer.iterator(); i.hasNext();) {
            currWord = i.next();
            if (currWord.startsWith(word)) {
                suggestions.add(currWord);
            }
        }
        return suggestions;
    }

    @Override
    public String toString() {
        return "{" + keyBuffer + "," + childTrackers + "}";
    }

//   public static void main( String[] args){
//      Collection exclList = new HashSet();
//      exclList.add("            q we");
//      exclList.add("asd");
//      System.out.println(exclList.contains("            q we"));
//      System.out.println(exclList.contains("q we"));
//      System.out.println(exclList.contains("ggggggggg"));
//   }

}