package com.milkcow.tripai.article.domain;

import com.milkcow.tripai.article.dto.ArticleCreateRequest;
import com.milkcow.tripai.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String locationName;

    @Column(nullable = false)
    private String formattedAddress;

    @Column
    private String image;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createDate;

    @UpdateTimestamp
    @Column
    private LocalDateTime modifyDate;

    // 엔티티 생성 메서드
    public static Article of(ArticleCreateRequest request, Member member) {

        return Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .locationName(request.getLocationName())
                .formattedAddress(request.getFormattedAddress())
                .image(request.getImage())
                .member(member)
                .build();
    }
}
