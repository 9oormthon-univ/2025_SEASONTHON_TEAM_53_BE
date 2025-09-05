package taro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taro.domain.JobCategory;
import taro.domain.Personality;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meta")
@Tag(name = "Meta", description = "메타데이터 조회 API")
public class MetaController {
    
    @Operation(summary = "직업 분야 옵션 조회", description = "선택 가능한 직업 분야 목록을 조회합니다.")
    @GetMapping("/job-options")
    public ResponseEntity<List<String>> getJobOptions() {
        List<String> options = Arrays.stream(JobCategory.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(options);
    }
    
    @Operation(summary = "성향 옵션 조회", description = "선택 가능한 성향 목록을 조회합니다.")
    @GetMapping("/personality-options")
    public ResponseEntity<List<String>> getPersonalityOptions() {
        List<String> options = Arrays.stream(Personality.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(options);
    }
}
