alter table ai_models add column size bigint;
alter table ai_models add column number_of_parameters bigint;
alter table ai_models add column quantization enum ('FP16','Q8','Q6','Q5','Q4');