package com.microservices.usuario.records;

import com.microservices.usuario.entity.Usuario;

public record UserWithToken(Usuario user, String accessToken) {
}
