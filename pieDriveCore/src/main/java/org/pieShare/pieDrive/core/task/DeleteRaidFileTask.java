/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Provider;
import org.pieShare.pieDrive.adapter.exceptions.AdaptorException;
import org.pieShare.pieDrive.core.AdapterCoreService;
import org.pieShare.pieDrive.core.PieDriveCore;
import org.pieShare.pieDrive.core.database.Database;
import org.pieShare.pieDrive.core.model.AdapterChunk;
import org.pieShare.pieDrive.core.model.AdapterId;
import org.pieShare.pieDrive.core.model.PhysicalChunk;
import org.pieShare.pieDrive.core.model.PieRaidFile;
import org.pieShare.pieDrive.core.stream.BoundedOutputStream;
import org.pieShare.pieDrive.core.stream.NioOutputStream;
import org.pieShare.pieDrive.core.stream.util.StreamFactory;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.IExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.api.task.IPieTask;

/**
 *
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class DeleteRaidFileTask implements IPieTask {

    private File file;
    private PieRaidFile pieRaidFile;

    private IExecutorService executorService;
    private PieDriveCore driveCoreService;
    private AdapterCoreService adapterCoreService;
    //todo: will need abstraction when merging into PieShare
    private Database database;

    private Provider<AdapterChunk> adapterChunkProvider;
    private Provider<UploadChunkTask> uploadChunkTaskProvider;

    public void run() {

        //ToDo: Converting to array becuase i want to delete during iteration of the list. 
        //This is slow. .. Maybe there is something better.
        for (PhysicalChunk physicalChunk : pieRaidFile.getChunks().toArray(new PhysicalChunk[pieRaidFile.getChunks().size()])) {
            for (Entry<AdapterId, AdapterChunk> ff : physicalChunk.getChunks().entrySet()) {
                try {
                    adapterCoreService.getAdapter(ff.getKey()).delete(ff.getValue());
                    physicalChunk.removeAdapterChunk(ff.getKey());
                } catch (AdaptorException ex) {
                   //Nothing to do here. Because i know afterwards if something went wrong. 
                }
            }
            if(physicalChunk.getChunks().isEmpty())
                pieRaidFile.getChunks().remove(physicalChunk);
        }
        
        if(!pieRaidFile.getChunks().isEmpty())
        {
            throw new Error("Not all files could have been delted.");
        }
        
    }

    public void setPieRaidFile(PieRaidFile file) {
        this.pieRaidFile = file;
    }

    public void setExecutorService(IExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

}
