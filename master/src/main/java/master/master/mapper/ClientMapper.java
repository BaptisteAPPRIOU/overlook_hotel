package master.master.mapper;

import master.master.domain.Client;
import master.master.web.rest.dto.ClientDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    Client toEntity(ClientDto.Update dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ClientDto.Update dto, @MappingTarget Client entity);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fidelityPoint", source = "fidelityPoint")
    ClientDto.Info toDto(Client entity);
}
