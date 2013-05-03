/*
 * Copyright (c) 2012, RamsesA <ramsesakama@gmail.com>
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */

package rakama.worldtools;

import java.io.File;
import java.io.IOException;

public class WorldTools
{   
    /**
     * Creates a WorldManager instance, where rootDirectory points to the 
     * location of a Minecraft world's "level.dat" file.
     * 
     * @param rootDirectory directory location for a "level.dat" file
     * @return a WorldTools instance for the specified world
     * @throws IOException
     */
    public static WorldManager getWorldManager(File rootDirectory) throws IOException
    {
        WorldManager manager = new WorldManager();
        manager.setDirectory(rootDirectory);
        return manager;
    }
}