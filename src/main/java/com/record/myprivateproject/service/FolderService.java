package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.domain.RepositoryEntity;
import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.dto.AuditAction;
import com.record.myprivateproject.dto.TreeDtos;
import com.record.myprivateproject.repository.FileEntryRepository;
import com.record.myprivateproject.repository.FolderRepository;
import com.record.myprivateproject.repository.RepositoryEntityRepository;
import com.record.myprivateproject.repository.UserRepository;
import com.record.myprivateproject.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
public class FolderService {
    private final FolderRepository folderRepo;
    private final FileEntryRepository fileRepo;
    private final RepositoryEntityRepository repoRepo;
    private final UserRepository userRepo;
    private final AuthContext authContext;
    private final RepositoryEntityRepository repositoryEntityRepository;
    private final AuditService auditService;

    public FolderService(FolderRepository folderRepo, RepositoryEntityRepository repoRepo, UserRepository userRepo, AuthContext authContext, RepositoryEntityRepository repositoryEntityRepository, FileEntryRepository fileRepo, AuditService auditService) {
        this.folderRepo = folderRepo;
        this.fileRepo = fileRepo;
        this.repoRepo = repoRepo;
        this.userRepo = userRepo;
        this.authContext = authContext;
        this.repositoryEntityRepository = repositoryEntityRepository;
        this.auditService = auditService;
    }
    public record Item(Long id, String name){}
    public record FileItem(Long id, String name, Long size, String contentType, Integer version){}
    public record Contents(Item folder, List<Item> folders, List<FileItem> files){}

    public Contents rootContents(Long repoId ){
        Folder root = folderRepo.findByRepositoryIdAndParentIsNull(repoId)
                .orElseThrow(() -> new IllegalArgumentException("루트 폴더가 없습니다."));
        return contents(root.getId());
    }

    public Contents contents(Long folderId){
        Folder folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더가 존재하지 않습니다."));
        List<Item> subFolders = folderRepo.findByParentOrderByIdAsc(folder).stream()
                .map(f -> new Item(f.getId(), f.getName()))
                .toList();
        List<FileItem> files = fileRepo.findByFolderOrderByIdAsc(folder).stream()
                .map(fe -> new FileItem(
                        fe.getId(),
                        fe.getName(),
                        fe.getSize(),
                        fe.getContentType(),
                        fe.getLatestVersion() == null ? null: fe.getLatestVersion().getVersionNo()
                ))
                .toList();
        return new Contents(new Item(folder.getId(), folder.getName()), subFolders, files);
    }
    @Transactional
    public Folder createFolder(Long repoId, Long parentId, String name) {
        final Long userId = authContext.currentUserId();
        RepositoryEntity repo = (repoId == null)
                ? getOrCreateDefaultRepo(userId)
                : repositoryEntityRepository
                .findByIdAndOwnerId(repoId, userId)
                .orElseThrow(()-> new IllegalArgumentException("repo를 찾을 수 없음"));

        Folder parent = null;
        if (parentId != null) {
            parent = folderRepo.findById(parentId)
                    .orElseThrow(()-> new IllegalArgumentException("parent 폴더를 찾을 수 없습니다."));
            if (!parent.getRepository().getId().equals(repo.getId())) {
                throw new IllegalArgumentException("상위폴더가 저장소에 존재하지 않습니다.");
            }
        }
        Folder folder = new Folder(repo, parent, name);
        folderRepo.save(folder);

        auditService.record(
                AuditAction.FOLDER_CREATE.name(),
                "FOLDER",
                folder.getId(),
                "name = " + folder.getName()
        );
        return folder;
    }

    @Transactional(readOnly = true)
    public List<Folder> children(Long folderId) {
        Folder parent = folderRepo.findById(folderId).orElseThrow();
        return folderRepo.findByParentOrderByIdAsc(parent);
    }

    private RepositoryEntity getOrCreateDefaultRepo(Long userId) {
        List<RepositoryEntity> existing = repoRepo.findByOwnerId(userId);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        User owner = userRepo.getReferenceById(userId);
        RepositoryEntity repo = new RepositoryEntity(owner, "DefaultRepository", "private");
        return repoRepo.save(repo);
    }
   @Transactional(readOnly = true)
   public List<Folder> rootFolders() {
        Long userId = authContext.currentUserId();
        Long repoId = repoRepo.findFirstByOwnerIdOrderByIdAsc(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 저장소가 없습니다."))
                .getId();

        return folderRepo.findByRepositoryIdAndParentIsNullOrderByIdAsc(repoId);
   }

   @Transactional(readOnly = true)
    public TreeDtos.FolderNode getTree(Long folderId, int depth) {
        if (depth<1) depth=1;
        Folder root = folderRepo.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        return buildNode(List.of(root), depth).get(0);
   }

   private List<TreeDtos.FolderNode> buildNode(List<Folder> roots, int depth) {
        var filesNow = fileRepo.findByFolderInOrderByIdAsc(roots);
        Map<Long, List<TreeDtos.FileNode>> filesByFolder = filesNow.stream()
                .collect(Collectors.groupingBy(f -> f.getFolder().getId(),
                        Collectors.mapping(f -> new TreeDtos.FileNode(
                                f.getId(),f.getName(),f.getSize(), f.getContentType(),
                                f.getLatestVersion() != null ? f.getLatestVersion().getVersionNo():null),
                                Collectors.toList())));
        List<TreeDtos.FolderNode> nodes = new ArrayList<>();
        if (depth == 1){
            for (Folder folder : roots){
                nodes.add(new TreeDtos.FolderNode(
                        folder.getId(),
                        folder.getName(),
                        folder.getParent() == null ? null :folder.getParent().getId(),
                        filesByFolder.getOrDefault(folder.getId(), List.of()),
                        List.of()
                ));
            }
            return nodes;
        }
        var children = folderRepo.findByParentInOrderByIdAsc(roots);
        Map<Long, List<Folder>> childMap = children.stream()
                .collect(Collectors.groupingBy(f-> f.getParent().getId()));

        Map<Long, List<TreeDtos.FolderNode>> childNodeMap = new HashMap<>();
        if (!children.isEmpty()){
            var childNodes = buildNode(children, depth -1);
            childNodes.forEach(n ->
                    childNodeMap.computeIfAbsent(n.parentId(), k -> new ArrayList<>()).add(n));
        }
        for (Folder folder : roots){
            nodes.add(new TreeDtos.FolderNode(
                    folder.getId(),
                    folder.getName(),
                    folder.getParent() == null ? null : folder.getParent().getId(),
                    filesByFolder.getOrDefault(folder.getId(), List.of()),
                    childNodeMap.getOrDefault(folder.getId(), List.of())
            ));
        }
        return nodes;
   }
}
