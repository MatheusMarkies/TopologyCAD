package com.brasens.model.io;

import com.brasens.model.report.ProjectData;
import com.brasens.model.objects.TopoObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectSaveState {
    private ProjectData projectData;
    private List<TopoObject> mapObjects;
}