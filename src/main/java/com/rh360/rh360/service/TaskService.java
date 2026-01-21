package com.rh360.rh360.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rh360.rh360.dto.TaskRequest;
import com.rh360.rh360.dto.TaskResponse;
import com.rh360.rh360.entity.Task;
import com.rh360.rh360.entity.User;
import com.rh360.rh360.entity.Team;
import com.rh360.rh360.repository.TaskRepository;
import com.rh360.rh360.repository.UsersRepository;
import com.rh360.rh360.repository.TeamRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UsersRepository usersRepository;
    private final TeamRepository teamRepository;

    public TaskService(TaskRepository taskRepository, UsersRepository usersRepository, TeamRepository teamRepository) {
        this.taskRepository = taskRepository;
        this.usersRepository = usersRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "pending");
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setCreatedAt(LocalDateTime.now().toString());
        task.setUpdatedAt(LocalDateTime.now().toString());
        task.setSubtasks(new ArrayList<>());
        
        // Definir usuário responsável se fornecido
        if (request.getResponsibleUserId() != null) {
            User user = usersRepository.findById(request.getResponsibleUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            task.setResponsibleUser(user);
        }
        
        // Definir equipe se fornecida
        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
            task.setTeam(team);
        }
        
        // Definir tarefa pai se fornecida
        if (request.getParentTaskId() != null) {
            Task parentTask = taskRepository.findById(request.getParentTaskId())
                .orElseThrow(() -> new RuntimeException("Tarefa pai não encontrada"));
            task.setParentTask(parentTask);
        }
        
        Task savedTask = taskRepository.save(task);
        return new TaskResponse(savedTask, true, true);
    }

    public Page<TaskResponse> findAll(Pageable pageable) {
        Page<Task> taskPage = taskRepository.findAll(pageable);
        List<TaskResponse> taskResponses = taskPage.getContent().stream()
            .map(task -> new TaskResponse(task, false, false))
            .collect(Collectors.toList());
        return new PageImpl<>(taskResponses, pageable, taskPage.getTotalElements());
    }

    public Page<TaskResponse> findByUserId(UUID userId, Pageable pageable) {
        List<Task> allTasks = taskRepository.findByResponsibleUserId(userId);
        
        if (allTasks.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        // Aplicar ordenação se especificada
        if (pageable.getSort().isSorted()) {
            Sort sort = pageable.getSort();
            allTasks.sort((t1, t2) -> {
                for (Sort.Order order : sort) {
                    int comparison = 0;
                    String property = order.getProperty();
                    comparison = compareTasks(t1, t2, property);
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Aplicar paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTasks.size());
        List<Task> paginatedTasks = start < allTasks.size() ? allTasks.subList(start, end) : new ArrayList<>();
        
        List<TaskResponse> taskResponses = paginatedTasks.stream()
            .map(task -> new TaskResponse(task, false, false))
            .collect(Collectors.toList());
        
        return new PageImpl<>(taskResponses, pageable, allTasks.size());
    }

    public Page<TaskResponse> findByTeamId(UUID teamId, Pageable pageable) {
        List<Task> allTasks = taskRepository.findByTeamId(teamId);
        
        if (allTasks.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        // Aplicar ordenação se especificada
        if (pageable.getSort().isSorted()) {
            Sort sort = pageable.getSort();
            allTasks.sort((t1, t2) -> {
                for (Sort.Order order : sort) {
                    int comparison = compareTasks(t1, t2, order.getProperty());
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Aplicar paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTasks.size());
        List<Task> paginatedTasks = start < allTasks.size() ? allTasks.subList(start, end) : new ArrayList<>();
        
        List<TaskResponse> taskResponses = paginatedTasks.stream()
            .map(task -> new TaskResponse(task, false, false))
            .collect(Collectors.toList());
        
        return new PageImpl<>(taskResponses, pageable, allTasks.size());
    }

    public Page<TaskResponse> findSubtasks(UUID parentTaskId, Pageable pageable) {
        List<Task> allTasks = taskRepository.findByParentTaskId(parentTaskId);
        
        if (allTasks.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        // Aplicar ordenação se especificada
        if (pageable.getSort().isSorted()) {
            Sort sort = pageable.getSort();
            allTasks.sort((t1, t2) -> {
                for (Sort.Order order : sort) {
                    int comparison = compareTasks(t1, t2, order.getProperty());
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Aplicar paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTasks.size());
        List<Task> paginatedTasks = start < allTasks.size() ? allTasks.subList(start, end) : new ArrayList<>();
        
        List<TaskResponse> taskResponses = paginatedTasks.stream()
            .map(task -> new TaskResponse(task, false, false))
            .collect(Collectors.toList());
        
        return new PageImpl<>(taskResponses, pageable, allTasks.size());
    }

    public Page<TaskResponse> findRootTasks(Pageable pageable) {
        List<Task> allTasks = taskRepository.findByParentTaskIsNull();
        
        if (allTasks.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        // Aplicar ordenação se especificada
        if (pageable.getSort().isSorted()) {
            Sort sort = pageable.getSort();
            allTasks.sort((t1, t2) -> {
                for (Sort.Order order : sort) {
                    int comparison = compareTasks(t1, t2, order.getProperty());
                    if (comparison != 0) {
                        return order.isAscending() ? comparison : -comparison;
                    }
                }
                return 0;
            });
        }
        
        // Aplicar paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTasks.size());
        List<Task> paginatedTasks = start < allTasks.size() ? allTasks.subList(start, end) : new ArrayList<>();
        
        List<TaskResponse> taskResponses = paginatedTasks.stream()
            .map(task -> new TaskResponse(task, false, false))
            .collect(Collectors.toList());
        
        return new PageImpl<>(taskResponses, pageable, allTasks.size());
    }

    public TaskResponse findById(UUID id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        return new TaskResponse(task, true, true);
    }

    @Transactional
    public TaskResponse update(UUID id, TaskRequest request) {
        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        if (request.getTitle() != null) {
            existingTask.setTitle(request.getTitle());
        }
        
        if (request.getDescription() != null) {
            existingTask.setDescription(request.getDescription());
        }
        
        existingTask.setStartDate(request.getStartDate());
        existingTask.setEndDate(request.getEndDate());
        existingTask.setUpdatedAt(LocalDateTime.now().toString());
        
        if (request.getStatus() != null) {
            existingTask.setStatus(request.getStatus());
        }
        
        // Atualizar usuário responsável se fornecido
        if (request.getResponsibleUserId() != null) {
            User user = usersRepository.findById(request.getResponsibleUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            existingTask.setResponsibleUser(user);
        }
        // Nota: Para remover o usuário responsável, envie um valor específico ou use endpoint dedicado
        
        // Atualizar equipe se fornecida
        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Equipe não encontrada"));
            existingTask.setTeam(team);
        }
        
        // Atualizar tarefa pai se fornecida
        if (request.getParentTaskId() != null) {
            Task parentTask = taskRepository.findById(request.getParentTaskId())
                .orElseThrow(() -> new RuntimeException("Tarefa pai não encontrada"));
            // Verificar se não está criando referência circular
            if (parentTask.getId().equals(id)) {
                throw new RuntimeException("Uma tarefa não pode ser pai de si mesma");
            }
            existingTask.setParentTask(parentTask);
        }
        
        Task savedTask = taskRepository.save(existingTask);
        return new TaskResponse(savedTask, true, true);
    }

    @Transactional
    public void delete(UUID id) {
        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        // Soft delete - marca como deletado
        existingTask.setStatus("deleted");
        existingTask.setUpdatedAt(LocalDateTime.now().toString());
        existingTask.setDeletedAt(LocalDateTime.now().toString());
        taskRepository.save(existingTask);
    }

    private int compareTasks(Task t1, Task t2, String property) {
        int comparison = 0;
        switch (property) {
            case "title":
                comparison = (t1.getTitle() != null && t2.getTitle() != null) 
                    ? t1.getTitle().compareToIgnoreCase(t2.getTitle()) : 0;
                break;
            case "status":
                comparison = (t1.getStatus() != null && t2.getStatus() != null) 
                    ? t1.getStatus().compareToIgnoreCase(t2.getStatus()) : 0;
                break;
            case "startDate":
                comparison = (t1.getStartDate() != null && t2.getStartDate() != null) 
                    ? t1.getStartDate().compareTo(t2.getStartDate()) : 0;
                break;
            case "endDate":
                comparison = (t1.getEndDate() != null && t2.getEndDate() != null) 
                    ? t1.getEndDate().compareTo(t2.getEndDate()) : 0;
                break;
            case "createdAt":
                comparison = (t1.getCreatedAt() != null && t2.getCreatedAt() != null) 
                    ? t1.getCreatedAt().compareTo(t2.getCreatedAt()) : 0;
                break;
            case "updatedAt":
                comparison = (t1.getUpdatedAt() != null && t2.getUpdatedAt() != null) 
                    ? t1.getUpdatedAt().compareTo(t2.getUpdatedAt()) : 0;
                break;
        }
        return comparison;
    }
}
