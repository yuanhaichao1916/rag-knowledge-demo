package com.demo.rag.service;

import com.demo.rag.entity.DocChunk;
import com.demo.rag.entity.dto.DocUploadDTO;

import java.util.List;

/**
 * 文档管理服务
 */
public interface DocService {

    /**
     * 上传文本：分块、向量化并批量入库
     *
     * @param dto 上传参数
     * @return 文档ID
     */
    String upload(DocUploadDTO dto);

    /**
     * 根据文档ID查询全部分片
     *
     * @param docId 文档ID
     * @return 分片列表
     */
    List<DocChunk> listByDocId(String docId);

    /**
     * 根据文档ID删除文档及其全部分片
     *
     * @param docId 文档ID
     * @return 删除的分片数量
     */
    int deleteByDocId(String docId);
}
