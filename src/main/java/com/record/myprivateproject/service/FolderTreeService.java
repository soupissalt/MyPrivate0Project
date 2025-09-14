package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.dto.TreeDtos;
import com.record.myprivateproject.repository.FileEntryRepository;
import com.record.myprivateproject.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FolderTreeService {

    private final FolderRepository folderRepo;
    private final FileEntryRepository fileEntryRepo;

    public FolderTreeService(FolderRepository folderRepo, FileEntryRepository fileEntryRepo) {
         this.folderRepo = folderRepo;
         this.fileEntryRepo = fileEntryRepo;
    }

    @Transactional
    public TreeDtos getFolderTree(Long rootFolderId) {
        Folder root = folderRepo.findById(rootFolderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        return mapFolderNode(root);
    }

    private TreeDtos mapFolderNode(Folder folder) {
        List<Folder> subFolders = folderRepo.findByParentIdOrderByNameAsc(folder.getId());

        List<TreeDtos> children = new ArrayList<>();
        for (Folder subFolder : subFolders) {
            children.add(mapFolderNode(subFolder));
        }

        List<FileEntry> files = fileEntryRepo.findByFolderIdOrderByNameAsc(folder.getId());
        for (FileEntry fe : files) {
            children.add(new TreeDtos(fe.getId(), fe.getName(), "FILE", List.of()));
        }

        children.sort(
                Comparator
                        .comparing((TreeDtos t) -> !"FOLDER".equals(t.getContentType()))
                        .thenComparing(TreeDtos::getName, String.CASE_INSENSITIVE_ORDER)
        );

        // <-- 여기만 바꾸기: builder() 대신 생성자 사용
        return new TreeDtos(folder.getId(), folder.getName(), "FOLDER", children);
    }

}
