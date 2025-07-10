package org.example.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "crime")
public class Crime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(name = "sentence_duration", nullable = true)
    private Integer sentenceDuration;

    @Column(name = "sentence_start_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date sentenceStartDate;

    @ManyToOne
    @JoinColumn(name = "prisoner_id")
    private Prisoner prisoner;

    public Crime() {}

    public Crime(String name) {
        this.name = name;
    }

    public void setPrisoner(Prisoner prisoner) {
        this.prisoner = prisoner;
    }
}
