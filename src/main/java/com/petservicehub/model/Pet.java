package com.petservicehub.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Animal de estimação cadastrado no PetServiceHub")
public class Pet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(nullable = false, length = 50)
	private String especie;

	@Column(length = 80)
	private String raca;

	@Column(nullable = false)
	private Integer idade;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "tutor_id", nullable = false)
	private Tutor tutor;

	@Column(nullable = false)
	@Builder.Default
	private Boolean ativo = true;
}
