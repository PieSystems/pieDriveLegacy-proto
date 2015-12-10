/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.core.database.repository;

import java.util.List;
import org.pieShare.pieDrive.core.model.ui.PieFolder;

/**
 *
 * @author richy
 */
public interface FolderEntityRepositoryCustom {

    void persistFolder(PieFolder folder);

    PieFolder findFolderByUid(String uid);

    List<PieFolder> findAllFolders();
}
