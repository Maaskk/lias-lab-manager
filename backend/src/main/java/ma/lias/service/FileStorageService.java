package ma.lias.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

@Service
public class FileStorageService {
  @Value("${app.upload-dir:uploads}") private String uploadDir;
  private static final long MAX = 10L * 1024L * 1024L;
  private static final Set<String> ALLOWED = Set.of("application/pdf","image/png","image/jpeg","image/webp","application/zip","text/plain","application/vnd.openxmlformats-officedocument.wordprocessingml.document");
  public String save(MultipartFile file, String folder) throws IOException {
    if(file == null || file.isEmpty()) return null;
    if(file.getSize() > MAX) throw new IllegalArgumentException("Fichier trop volumineux. Taille maximale : 10MB.");
    String contentType = file.getContentType()==null?"application/octet-stream":file.getContentType();
    if(!ALLOWED.contains(contentType)) throw new IllegalArgumentException("Type de fichier non autorisé : "+contentType);
    String clean = file.getOriginalFilename()==null?"file":file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]","_");
    Path dir = Path.of(uploadDir, folder); Files.createDirectories(dir);
    String stored = System.currentTimeMillis()+"_"+clean;
    Files.copy(file.getInputStream(), dir.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
    return "/uploads/"+folder+"/"+stored;
  }
}
