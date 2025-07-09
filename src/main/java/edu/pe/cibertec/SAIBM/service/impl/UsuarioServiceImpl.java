package edu.pe.cibertec.SAIBM.service.impl;

import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import edu.pe.cibertec.SAIBM.repository.UsuarioRepository;
import edu.pe.cibertec.SAIBM.service.UsuarioService;
import edu.pe.cibertec.SAIBM.util.HashUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl extends GenericServiceImpl<UsuarioEntity, Integer> implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostConstruct
    private void init() {
        super.repository = usuarioRepository;
    }

    @Override
    public List<UsuarioEntity> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre);
    }


    @Override
    public Optional<UsuarioEntity> encontrarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    @Override
    @Transactional
    public Optional<UsuarioEntity> validarLogin(String correo, String contraseña) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isPresent()) {
            UsuarioEntity usuario = usuarioOpt.get();
            String hashedInputPassword = HashUtil.Nuevo(contraseña);

            if (usuario.getContraseña().equals(hashedInputPassword)) {
                usuario.getPerfil().getAccesos().size();
                return usuarioOpt;
            }
        }

        return Optional.empty();
    }


    @Override
    public UsuarioEntity crear(UsuarioEntity usuario) {
        if (usuario.getId() == null || usuario.getId() == 0) {
            usuario.setContraseña(HashUtil.Nuevo(usuario.getContraseña()));
        } else {
            Optional<UsuarioEntity> usuarioActualOpt = usuarioRepository.findById(usuario.getId());

            if (usuarioActualOpt.isPresent()) {
                UsuarioEntity usuarioActual = usuarioActualOpt.get();

                if (!usuarioActual.getContraseña().equals(usuario.getContraseña())) {
                    usuario.setContraseña(HashUtil.Nuevo(usuario.getContraseña()));
                }
            } else {
                usuario.setContraseña(HashUtil.Nuevo(usuario.getContraseña()));
            }
        }

        return usuarioRepository.save(usuario);
    }


}
