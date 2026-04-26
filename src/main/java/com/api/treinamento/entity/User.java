package com.api.treinamento.entity;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Builder 
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User implements UserDetails {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	
	private String fullName;
	private String email;
	private String password;
	private String phone;
	
	@Enumerated(EnumType.STRING)
	private Role role;
	
	private Date lastLoginDate;
	private Date lastLoginDateDisplay;
	private Date joinDate;
	private boolean isActive = true;
	private boolean isNotLocked = true;
	private boolean isChangePassword = true;
	
	@Override
	public String getUsername() {
		return this.email;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return this.isNotLocked;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return this.isActive;
	}
}