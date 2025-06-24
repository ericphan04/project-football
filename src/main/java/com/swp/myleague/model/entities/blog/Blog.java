package com.swp.myleague.model.entities.blog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.swp.myleague.model.entities.Comment;
import com.swp.myleague.model.entities.information.Club;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Blog {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID blogId;

    private String blogTitle;
    private String blogContent;
    private LocalDateTime blogDateCreated;
    private String blogThumnailPath;

    @Enumerated(EnumType.STRING)
    private BlogCategory blogCategory;

    @OneToMany(mappedBy = "blog")
    private List<Comment> comments;

    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

}
